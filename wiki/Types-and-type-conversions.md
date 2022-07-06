## Newly supported Types
So far the open-source LLVM trunk has not yet implemented bit size support larger than 128bits. We have implemented 256bit supports in our own backend, and is considering contributing them back to main trunk.

Users are allowed to use `i256` and `i160` data types in their generated LLVM IR, which represent 256bit integer types and 160bit integer types respectively.

Even though all EVM data types are 256bit in length internally. We are still able to offer support to smaller data types. However, users are encouraged to use 256bit data types internally because it is free.

## Contract Input Argument Types -- The Solidity convention 

Contract arguments are passed to EVM via the call data field. The function dispatcher is responsible to extract input arguments from call data. 

In Solidity's convention, the arguments in call data are padded to 32 bytes long if its data type's length is shorter. So, in order to maintain the convention, the function dispatcher needs to truncate the input arguments to the defined size in the function that is going to be called.

This is undoubtedly inefficient, so users are discouraged to use smaller data types. 
