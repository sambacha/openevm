## Vyper
### Internal (private) calls
Codegen Steps (inside `self_call.py`):
1. push current local variables
2. push arguments
3. push jumpdest (callback ptr)
4. jump to label
5. pop return values
6. pop local variables

A function invocation is generated using this stub:
```
add (pc) 6
goto func_label
jumpdest
``` 

A call body is consists of the following (as extracted from `self_call.py:call_self_private`):
1. pre_init
2. `push_local_vars`: if there are local variables
3. `push_args`
4. `jump_to_func`
5. `pop_return_values`
6. `pop_local_vars`

If there is dynamic section in the variables, those arguments will be stored in memory. Here is the following code generated to for storing variables in memory space:
```
#
mstore placeholder (argument_start_pos + argument_size)
label_start

# compare if we have reached static arguments
(lt (mload placeholder) static_pos)
jumpi label_end

# compare if we have reached the counter to zero
(iszero (ne (mload (mload placeholder)) 0))
(mload (mload placeholder))

# decrease counter by 1
(mstore placeholder (sub (mload placeholder) 32)) 
jump label_start
label_end
```
* `placeholder` is a new memory time slot calculated statically
* `static_pos` is the end position of the static arguments
 
The static section of the arguments are generated using `mload`s.

