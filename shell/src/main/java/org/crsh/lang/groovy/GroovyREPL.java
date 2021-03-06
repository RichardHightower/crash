/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.crsh.lang.groovy;

import groovy.lang.GroovyShell;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.command.CommandContext;
import org.crsh.command.CommandInvoker;
import org.crsh.command.InvocationContextImpl;
import org.crsh.lang.groovy.closure.PipeLineInvoker;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.repl.EvalResponse;
import org.crsh.repl.REPL;
import org.crsh.repl.REPLSession;

import java.io.IOException;

/**
 * Groovy REPL implementation.
 *
 * @author Julien Viet
 */
public class GroovyREPL extends CRaSHPlugin<REPL> implements  REPL {

  @Override
  public REPL getImplementation() {
    return this;
  }

  public String getName() {
    return "groovy";
  }

  public EvalResponse eval(final REPLSession session, final String request) {
    CommandInvoker<Void, Object> invoker = new CommandInvoker<Void, Object>() {
      public void provide(Void element) throws IOException {
        throw new UnsupportedOperationException("Should not be invoked");
      }
      public Class<Void> getConsumedType() {
        return Void.class;
      }
      public void flush() throws IOException {
      }
      public Class<Object> getProducedType() {
        return Object.class;
      }
      CommandContext<Object> foo;
      public void open(CommandContext<? super Object> consumer) {
        this.foo = (CommandContext<Object>)consumer;
        GroovyShell shell = GroovyCommandManager.getGroovyShell(session.getContext(), session);
        ShellBinding binding = (ShellBinding)shell.getContext();
        binding.setCurrent(foo);
        Object o;
        try {
          o = shell.evaluate(request);
        }
        finally {
          binding.setCurrent(null);
        }
        if (o instanceof PipeLineInvoker) {
          PipeLineInvoker eval = (PipeLineInvoker)o;
          try {
            eval.invoke(new InvocationContextImpl<Object>(foo));
          }
          catch (Exception e) {
            throw new UnsupportedOperationException("handle me gracefully", e);
          }
        } else {
          try {
            if (o != null) {
              consumer.provide(o);
            }
          }
          catch (IOException e) {
            throw new UnsupportedOperationException("handle me gracefully", e);
          }
        }
      }
      public void close() throws IOException {
        foo.flush();
        foo.close();
      }
    };
    return new EvalResponse.Invoke(invoker);
  }

  public CompletionMatch complete(REPLSession session, String prefix) {
    return new CompletionMatch(Delimiter.EMPTY, Completion.create());
  }
}
