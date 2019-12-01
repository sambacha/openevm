It is the frontend's responsibility to do the smart contract's plumbing, including the contract's constructor function. 

### The Contract constructor function
* The constructor should be the first function in the generated LLVM IR.
* The constructor should be named "solidity.main" (will change to a more general name in the future).
* The constructor should not take any arguments. There is a special pass to transform the constructor function differently than other functions.

### Skeleton example of a very small constructor function
Here is an illustration of the skeleton of a small smart contract. In order to initialize the smart contract environment, we will initialize the free memory pointer (at address 0x40) at the very beginning. The frontend is expected to use intrinsic functions as the following 
```
declare void @llvm.evm.mstore(i256, i256)
declare void @llvm.evm.sstore(i256, i256)
declare i256 @llvm.evm.callvalue()
declare void @llvm.evm.revert(i256, i256)
declare void @llvm.evm.codecopy(i256, i256, i256)
declare void @llvm.evm.return(i256, i256)


@deploy.size = common global i256 0

define void @solidity.main() {
entry:
  call void @llvm.evm.mstore(i256 64, i256 128)
  call void @llvm.evm.sstore(i256 0, i256 1)
  %0 = call i256 @llvm.evm.callvalue()
  %cv = icmp eq i256 %0, 0
  br i1 %cv, label %Zero, label %NoZero
Zero:
  call void @llvm.evm.revert(i256 0, i256 0)
  br label %Finally
NoZero:
  %deploy_size = load i256, i256* @deploy.size
  call void @llvm.evm.codecopy(i256 0, i256 34, i256 %deploy_size)
  call void @llvm.evm.return(i256 0, i256 0)
  br label %Finally 
Finally:
  ret void
}
```

Notice that we cannot know the contract deploy size (%deploy_size variable) until binary generation is completed. So we will have to use a place holder for the IR representation. 