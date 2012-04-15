package org.crsh;

import org.crsh.command.ScriptException;
import org.crsh.shell.AbstractCommandTestCase;

import java.sql.SQLException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JDBCCommandTestCase extends AbstractCommandTestCase {

  public void testCannotConnect() {
    assertError("jdbc connect jdbc:foo", SQLException.class);
  }

  public void testNotConnected() {
    assertError("jdbc execute create table derbyDB(num int, addr varchar(40))", ScriptException.class);
  }

  public void testExecute() {
    System.setProperty("derby.connection.requireAuthentication", "true");
    System.setProperty("derby.authentication.provider", "BUILTIN");
    System.setProperty("derby.user.my_user", "my_password");
    assertOk("jdbc connect -u my_user -p my_password jdbc:derby:memory:EmbeddedDB;create=true");
    assertOk("jdbc execute create table derbyDB(num int, addr varchar(40))");
    assertOk("jdbc execute insert into derbyDB values (1956,'Webster St.')");
    String res = assertOk("jdbc execute select * from derbyDb").getText();
    assertTrue(res.contains("Webster"));
    assertOk("jdbc close");
  }

  public void testClose() {
    assertError("jdbc close", ScriptException.class);
  }
}
