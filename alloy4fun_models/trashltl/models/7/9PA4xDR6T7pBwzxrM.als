open main
pred id9PA4xDR6T7pBwzxrM_prop8 {
	always (all f:File| some f.^link  implies  eventually  f.^link  in Trash)

}
pred __repair { id9PA4xDR6T7pBwzxrM_prop8 }
check __repair { id9PA4xDR6T7pBwzxrM_prop8 <=> prop8o }