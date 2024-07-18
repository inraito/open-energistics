# ItemStack Descriptor
This class is used to provide information about an ItemStack,
typically acquired by [ME Switching Card](../../../component/MESwitchingCard.md).

```json
{
    "registry_name": "$registry_name:String",
    "num": "$num:int",
    "nbt": "$nbt:CompoundNBT"
}
```
In which the `nbt` field will be `{}` if the compound tag is null or empty.  