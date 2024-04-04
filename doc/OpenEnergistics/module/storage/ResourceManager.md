# Resource Manager(RM)

This class is a singleton interface for handling stacked resources(including ItemStacks).
By singleton we mean that for a open-energistics instance(system), it will have and only
have a single RM. It covers the physical heterogeneity and provide a unified api for the
instance to use.

All methods here are interface, not implementation. To put it another way, this doc
describes the exterior of a blackbox, and we don't care what its interior
looks like.

RM maintains a list of entries. Each entry looks like this:

| id | filter | count | occupied |  
|----|--------|-------|----------|

`id` is the basically the handler, very similar to a file handler in os.
`filter` is the description of this resource, and its format is a driver's issue,
not resources manager's. `count` is the number of available resources and lastly
`occupied` is the resources that are occupied but haven't been used.

And RM exposes the following apis to outside users.

`open(filter:ResFilter):ResHandler`:   
`close(handler:ResHandler):int`  
`count(handler:ResHandler):int`  
`occupy(count:int):int`  
`release(count:int):int`  
`exploit(count:int, target:ResTarget):int`  

`open()`creates an entry in this list, `close()` deletes the entry, `count()`
returns the count cell, `occupy()` deduct the requested number on count and
increase the same number on occupied cell while `release()` do it in the opposite direction.
And finally `exploit()` deduct the occupied cell and move the resources to where it will be consumed.  

### Detailed Description
`open()`   
RM analyzes the filter and searches all places within the system and likely, indexes them. 
Then create an entry described above in the list. This operation may or may not be idempotent,
that is, depending on the driver, an existent handler might be returned. If a new entry is created,
occupied cell will be set to zero.

`close()`  
RM delete the entry from the list and free all indexes and data related to that entry.  

`count()`  
RM calculated the number of this resource.

`occupy()`  
RM make the required number of resource become *occupied*, making them unable accessed by both
exterior accessors and interior other RM users. Beside that, RM also makes these resources ready
to be exploited, moving them to output caches for example.

`release()`  
Basically the undo for `occupy()`.

`exploit()`  
RM exploit the requested number of resource. Exhaust them in other words. To do that, first you
need to *occupy* them

### About Consistency
Consistency on operations through RM and Open-Energistics are guaranteed by lua's coroutine mechanics.
if you don't yield, no others can change the state of the RM themselves.

### Future Prospect
Asynchronous mechanics.

***
The following paragraphs introduce what our implementation of RM is.

### Driver
Firstly, we introduce 2 types of driver acting as adaptor used by the RM: 
FilterDriver and TargetDriver. The former provides the ability to evaluate the filter
and implements the first 5 methods, while the latter analyze the ResTarget
and implements the `exploit()` methods.

RM maintain a map from filter type to FilterDriver and a map from filter type and target
type to TargetDriver. Both ResFilter and ResTarget will be of the format `(type:string, data:obj)`.

Therefore, each call on the former 5 methods will be redirected to corresponding FilterDriver, and 
each call on `exploit()` will be redirected to corresponding TargetDriver, according to the
`type` in filter and target. 

Drivers should be added during the initialization phase.
