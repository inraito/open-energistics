package inraito.openerg.lua.dummy.api
import inraito.openerg.lua.dummy.DummyLuaEnvironment
import li.cil.oc.util.ScalaClosure.toSimpleJavaObjects
import li.cil.repack.org.luaj.vm2.{Globals, LuaValue, Varargs}
import li.cil.oc.util.ScalaClosure._

class ComponentAPI(luaEnv:DummyLuaEnvironment) extends DummyAPI {
  private val luaEnvironment = luaEnv
  def components: Map[String, String] = luaEnvironment.components

  override def initialize(globals: Globals) = {
    // Component interaction stuff.
    val component = LuaValue.tableOf()
  }
}
