open main
pred idmtkSWRRzaMx85aZ8c_prop20 {
 	always (all f:File | f in Trash since  (f not in Protected) )
}
pred __repair { idmtkSWRRzaMx85aZ8c_prop20 }
check __repair { idmtkSWRRzaMx85aZ8c_prop20 <=> prop20o }