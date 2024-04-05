# StorageSystem Manager(SM)
Manager for storage system. Note that it only manages those systems acting as storage
and cache for occupied ItemStacks. Storage systems that abstract machines are not included.
Similar to [RM](./ResourceManager.md), SM is also a singleton.

`store(addr:string, slot:int, num:int):int`   is the most important api in SM, in which
`addr` is the oc address of source storage system, `slot` is the slot index and the `num`
is the request number.

`get_storage():string[]`  
`get_cache():string[]`  
These two methods are basically used only by [StorageSystem filter driver].

***

### Registration of Storage System
Hot plugging is not supported, registration should be done using lua script during the
initialization phase. And the lua registration apis are as follows.

`register(addr:string, type:int):SMHandle`

#### Type of StorageSystem
There are two types of storage system within the SM for the time being.
One is storage and the other is occupy. The former one is for common storage purposes,
and the latter is used as cache for occupied ItemStacks, keeping other accessors off.

***

### Customization of Storage Logics
`set_global_selector(selector:GlobalSelector)`  
`set_local_selector(selector:LocalSelector)`  

GlobalSelector is called to query which storage system this ItemStack should be stored in.
And LocalSelector is called to query about which slot. So naturally, there is only one
GlobalSelector and each storage system could have a LocalSelector. Note that it only affect
the behaviour of `store()`, that is moving ItemStacks in. Customizing the logic of *out*,
such as using RM is not supported, though you may achieve that by editing the logic of 
[StorageSystem filter driver].

[StorageSystem filter driver]: ./driver/filter/StorageSystemFilter.md

