## Variables

In the context of stack machine, a variable refers to an operand that will be consumed by an opcode. In EVM LLVM, variables are treated as virtual registers, until they are _stackfied_ (convert register-based code to stack-based code) right before lowering to machine code.

In LLVM's internal SSA representation mode, it is fairly easy to compute a register's live range (the range from its assignment to its last use). Variables are treated differently with regard to its live range. Local variables (variables that its liveness only extends within a single basic block) will live entirely on the stack, while non-local variables (variables that live across basic blocks) will be spilled to a memory slot allocated by the compiler.

#### Frame Objects

Frame objects will be allocated either on stack or on memory space. Since each of the elements are 256bits, we have to ensure that frame objects are 256bits in length as well. Frame objects with smaller length is not supported.

It is possible for a frame object to be allocated on to memory space, if we are consuming too much of stack space. The stack allocation pass will try to find an efficient way to decide which goes to the memory and which stays in stack.

### Frame Pointer (or Free Memory Pointer)

[Stack pointers and frame pointers](https://en.wikipedia.org/wiki/Call_stack#Stack_and_frame_pointers) are essential to support subroutine calls. Frame pointer is used to record the structure of stack frames. Because we do not have registers in EVM, we will have to store stack frame pointer in memory locations. Usually, we put stack frame pointer at location `0x40`, and we follow Solidity compiler's convention to initialize it to value `128`. So the stack frame of the first function starts at that location. The value of frame pointer changes as the contract calls a subroutine or exits from a subroutine. Whenever we need to have access to frame pointer, we will retrieve its value from that specific location.

### Memory stack

Part of the memory is used as a stack for function calls and variable spills. The structure is described as follows:

-   The stack goes from lower address to higher address, as different from usual hardware implementations.
-   The frame is arranged into 3 parts:
    -   **frame object locations**. Each frame object has its own frame slot. Frame object `x` will have a 32 byte space starting from `$fp + (x * 32)`, where `$fp` is the frame pointer, and is stored at location `0x40`.
    -   **spilled variables**. Variable that are unable to be fully stackified will reside on the memory stack. In codegen, each spilled variable will have an index, and each index refers to a memory slot. A spilled variable that bears index `y`, will reside at location `$fp + (number_of_frame_objects * 32) + (y * 32)`.
    -   **subroutine context**. Like a regular register machine, the memory stack is used to store subroutine context so as to support function calls. Two slots are allocated at the end of current frame for a) the existing frame pointer, and b) return `PC` address.

Here is an example showing a stack frame right before we jump into a subroutine:

```
  Stack top                                    Higher address
 +-----------> +----------------------------+ <--------------+
               |                            |
               |     Return Address         |
               |                            |
               +----------------------------+
               |                            |
               |     Function argument      |
   new FP      |                            |
 +-----------> +----------------------------+
               |                            |
               |    Saved frame pointer     |
               |     (Start of frame)       |
               +----------------------------+
               |                            |
               |     Stack Object 1         |
               |                            |
               +----------------------------+
               |                            |
               |     Frame Object 2         |
               |                            |
               +----------------------------+
               |                            |
               |     Frame Object 1         |
Start of frame |                            |   Lower address
+------------> +----------------------------+ <----------------+
```
