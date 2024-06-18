package inraito.openerg.lua.dummy

import inraito.openerg.lua.dummy.api.ComponentAPI
import li.cil.repack.org.luaj.vm2.Globals
import li.cil.repack.org.luaj.vm2.lib.jse.JsePlatform

import java.util

class DummyLuaEnvironment {
    def components:Map[String, String] = {Map.empty[String, String]}

    private def insertAPI(globals : Globals):Unit = {
        new ComponentAPI(this).initialize(globals)
    }

    def create():Globals = {
        val globals = JsePlatform.standardGlobals()
        insertAPI(globals)
        globals
    }
}
