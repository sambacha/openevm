## Variables

In the context of stack machine, a variable refers to an operand that will be consumed by an opcode. In EVM LLVM, variables are treated as virtual registers, until they are *stackfied* (convert register-based code to stack-based code) right before lowering to machine code. 

In LLVM's internal SSA representation mode, it is fairly easy to compute a register's live range (the range from its assignment to its last use). Variables are treated differently with regard to its live range. Local variables (variables that its liveness only extends within a single basic block) will live entirely on the stack, while non-local variables (variables that live across basic blocks) will be spilled to a memory slot allocated by the compiler.

### Memory stack

Part of the memory is used as a stack for function calls and variable spills. The structure is described as follows:
* The free memory pointer (or frame pointer) is the base pointer of the frame for this function, it is located at address `0x40`. Usually, we follow Solidity compiler's convention to initialize it to value `128`. So the stack frame of the first function starts at that location. The value of frame pointer changes as the contract calls a subroutine or exits from a subroutine.
* The frame is arranged into 3 parts:
    * **frame object locations**. Each frame object has its own frame slot. Frame object `x` will have a 32 byte space starting from `$fp + (x * 32)`, where `$fp` is the frame pointer, and is stored at location `0x40`.
    * **spilled variables**. Variable that are unable to be fully stackified will reside on the memory stack. In codegen, each spilled variable will have an index, and each index refers to a memory slot. A spilled variable that bears index `y`, will reside at location `$fp + (number_of_frame_objects * 32) + (y * 32)`.
    * **subroutine context**. Like a regular register machine, the memory stack is used to store subroutine context so as to support function calls. Two slots are allocated at the end of current frame for a) the existing frame pointer, and b) return `PC` address. 

 