# Unit tests

Run the following command at the repository root with a JDK 8 installation selected via `JAVA_HOME`. ForgeGradle resolves the same mapped Forge, AE2 and OC dependencies as the mod build. The test task runs JUnit in a separate JVM. It does not launch a client, a dedicated server or a world.

```powershell
.\gradlew.bat test
```

Gradle tests all four combinations of OECore and OEMM in one invocation. Both floppies are packaged in one JAR. Run `.\gradlew.bat test renameReobfJar` to produce the installable mod under `build/renamedJar`. On Linux or macOS use `./gradlew`. The HTML report is under `build/reports/tests/test/index.html` and the XML results are under `build/test-results/test`.

## Maintaining an optional Lua check

Edit the Lua source on the existing `oecore` or `oemm` disk. Prefix each complete optional line with `--@check `, including declarations and state updates that serve only the check. For example, the scheduler contains this line inside `sleep`:

```lua
    --@check if not self.coroutines[id] then return false end
```

Use consecutive marked lines for a multiline check. Put the marker in Lua code, outside multiline strings and comments. Gradle expands these comments under `robust/` and replaces each marked line with a blank line under `performance/`. The line numbers match the original source in both editions. Inspect the outputs in `build/resources/main/assets/openerg/disk/<disk>/<edition>/`. The performance test compares every installed Lua script with the source after removing the marked lines.

Put new Lua cases under `src/test/lua/oecore` or `src/test/lua/oemm`. A plain `*_test.lua` runs with all four disk combinations. A suffix of `_performance_test.lua` or `_robust_test.lua` selects the edition of the component named by its directory. The other component is exercised in both editions. JUnit includes both edition names in each case. To run just the two homogeneous suites:

```powershell
.\gradlew.bat test --tests "*LuaDiskTest*oecore=performance, oemm=performance,*"
.\gradlew.bat test --tests "*LuaDiskTest*oecore=robust, oemm=robust,*"
```

## Java and Scala

Place JUnit 4 tests under `src/test/java` or `src/test/scala` in the package of the class under test. Use Mockito for external interactions and assert the outcome of the production method. To run one class, for example:

```powershell
.\gradlew.bat test --tests "*BlockControllerTest"
```

- The block controller test calls the production `map`, `check` and `unmap` methods. A Mockito partial mock bypasses construction of the registered block entity, supplies an OC virtual filesystem and suppresses the world dirty notification. A second controller reads the same disk to exercise persistence across controller instances. See [BlockControllerTest](../../src/test/scala/inraito/openerg/common/tileentity/BlockControllerTest.scala).
- The OC interface tests serialize a pending crafting message to Minecraft NBT and restore both pending and empty states. The storage protocol tests pass OC messages to the real receiver and verify which operations reach the storage backend. See [OCInterfaceContextTest](../../src/test/java/inraito/openerg/common/tileentity/OCInterfaceContextTest.java) and [StorageSystemReceiverTest](../../src/test/java/inraito/openerg/common/tileentity/storage/StorageSystemReceiverTest.java).
- The Scala disk test checks the resource paths computed by `DiskList` against the Lua library and initialization files in the processed mod resources. See [DiskListTest](../../src/test/scala/inraito/openerg/common/item/DiskListTest.scala).

## Lua disk software

Add a script ending in `_test.lua` anywhere under `src/test/lua`. The runner discovers it recursively and reports it as a separate JUnit case. Each script gets a fresh LuaJ VM, module cache, component table and event queue. Require modules by their OC names or use an absolute virtual disk path such as `dofile('/etc/oecore/crafting/ae_chips.lua')`. Both load the production files from the mod resources.

```powershell
.\gradlew.bat test --tests "*LuaDiskTest"
```

The [OC fixture](../../src/test/lua/opencomputers.lua) provides the following controls:

- `oc.signal(...)` queues an OpenComputers signal. The OE event puller consumes it during a scheduler pass.
- `oc.run(scheduler, passes)` advances the production scheduler for a fixed number of passes. Assert observable state after it returns. Scheduler passes are independent of wall-clock time.
- `oc.switchingCard(inventories)` creates a single-item transfer fixture for pipes. The caller supplies a table indexed first by component address and then by numeric slot. Each occupied slot contains a `registry_name` and `num`. Tests can change these inventories between passes to model machine input and output.

The [processor recipe test](../../src/test/lua/oecore/ae_chips_test.lua) loads the shipped recipe, delivers a modem message with an ingredient range and checks input and output slots on separate passes. The [pipe test](../../src/test/lua/oecore/pipe_test.lua) covers a blocked destination and transfer limits. The [OEMM test](../../src/test/lua/oemm/allocation_test.lua) exercises allocation, waiting for a machine, wake-up on release and shutdown.

The fixture models the OC operations these cases use. In-world block placement, AE2 network discovery, actual machine processing times and chunk lifecycle require separate integration coverage.
