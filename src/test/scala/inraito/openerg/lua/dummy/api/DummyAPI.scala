package inraito.openerg.lua.dummy.api

import li.cil.repack.org.luaj.vm2.Globals

abstract class DummyAPI(){
  def initialize(globals: Globals): Unit
}
