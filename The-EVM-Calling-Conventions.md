The EVM architecture is a simplistic structure, but it has everything we need to do usual program computations. 

## Types of calls

There are two types of calls in an EVM smart contract:
1. **Internal calls**. Internal calls are referred to function calls within a smart contract. An example is that we have two defined function `A` and `B`, and somewhere in `A` we save our context and change our execution flow to the beginning of `B`. 
2. **External calls**. Or cross-contract calls. `A` and `B` are defined in different deployed EVM contract and `A` calls `B` in its context.

## Internal call conventions
Up to ETH 1.5, there is no link and jump EVM opcode for easy handling of subroutines(even though some [discussions](https://github.com/ethereum/EIPs/issues/2315) are on-going). So we have to manually handle subroutine calls. Here are the calling conventions for an internal calls:
* current subroutine's frame pointer is saved at stack, at memory location `$fp - 32` where `$fp` is the subroutine call's frame pointer.
* arguments are all pushed on stack, along with the return address. Argument with smaller index number occupies a stack slot on top of another argument with a larger index number. For example, when we want to do a function call: `func abc(x, y, z)`, here is the arrangement of the arguments:
```
               +-----------+
               |Return Addr|
               +-----------+
               |     X     |
               +-----------+
               |     Y     |
               +-----------+
 Current FP    |     Z     |
+------------> +-----------+
               |  Old FP   |
               +-----------+
               |   .....   |
               +-----------+
```

*Note: Putting the return address on top of the stack is because it is easier to compute the location, but this will result in more stack manipulation overhead for the subroutine calls. We will improve this design in a later version.*
* A subroutine's return value is stored on stack top.
*Note: currently we only support one return value. In the future we will improve it by supporting multiple return values.*

## Procedure of a subroutine call
To illustrate the procedure for a subroutine call, we need to do the following to save the context of current function execution:
1. calculate the current frame size. The frame size should be the size sum of: a) slots occupied by frame objects, b) slots occupied by spilled variables, and c) one more slot for storing current frame pointer. let's assume the frame size is calculated to be `%frame_size`.
2. save existing frame pointer. The frame pointer resides at `0x40`.
3. bump the frame pointer to: `$fp = $fp + %frame_size`. After that, we can easily restore the old frame pointer by looking at location `$fp - 32`.
4. push all subroutine arguments in order on to stack.
5. push return address onto stack.
6. push the beginning address of subroutine and jump.

Right before we return from a subroutine, the stack should be empty and the return address should be at the top of the stack. When returning from a subroutine call, we should do the following:
1. push return value on to top of stack.
2. Do a `swap1` to move the return address to top of stack
3. jump to return address and resume the execution in caller function.
If the function returns nothing, simply jump to return address.

After jumping back to caller, we have to resume the execution:
1. restore caller's frame pointer by storing the value at location `$fp - 32` to `0x40`.

