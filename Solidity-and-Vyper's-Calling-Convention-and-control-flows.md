## Vyper

### Dynamic memory allocation
Memory address `0x00 ~ 0x200` (`0 ~ 320`) are reserved by Vyper compiler. new dynamic memory allocation uses memory locations starting from `0x200` (`320`).

### Internal public calls
This refers to calls that are public and within a same contract.

An `call` instruction is generated:
* gas price (3rd argument) is set to zero.
* Input arguments are packed and stored in memory. Its pointer is the 4th argument.

#### Packing arguments


#### Unpacking arguments
static arguments:
* `i_placeholder` : temporary memory space used as counter
* `begin_pos` : the beginning position of the packed arguments
stack arguments:
TODO
```
(mstore begin_pos stacktop_value)
(mstore i_placeholder 0)
(jumpdest start_label)
(if
  (ge
     (mload i_placeholder)
     (ceil32 (mload begin_pos))
  )
  (jump end_label)
)
(mstore
   (add (add begin_pos 32) (mload i_placeholder))
   stacktop_value_minus_1
)
(mstore i_placeholder (add 32 (mload i_place_holder)) # incrementing i
(jump start_label)
(jumpdest end_label)
```

### Internal private calls
This refers to calls that are declared private and within a same contract.
#### calling into a function
Codegen Steps (inside `self_call.py`):
1. push current local variables
2. push arguments
3. push jumpdest (callback ptr)
4. jump to label
5. pop return values
6. pop local variables

##### Function invocation
A function invocation is generated using this stub:
```
add (pc) 6
goto func_label
jumpdest
``` 

##### Sequence of a function call
A call body is consists of the following (as extracted from `self_call.py:call_self_private`):
1. `pre_init` : it is an empty code stub
2. `push_local_vars`: if there are local variables
3. `push_args`
4. `jump_to_func` : see section "Function invocation" above.
5. `pop_return_values` : see section "returning from a function" below.
6. `pop_local_vars` : bunch of `mstore`s.

##### dynamic section of arguments
Arguments in dynamic section are packed and stored in memory.
Here is the following code generated to for storing variables in memory space:
```
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

#### Returning from a function
The callee will generate an additional `pop` if the function's return type is `None`.


## Control Flows
#### `if x : y else z`
```
(session x)
(jumpi (iszero x) mid_symbol)
(session y)
(jump end_symbol)
(jumpdest mid_symbol)
(session z)
(jumpdest end_symbol)
```

#### `if x : y`
```
(session x)
(jumpi (iszero x) end_symbol)
(session y)
(jumpdest end_symbol)