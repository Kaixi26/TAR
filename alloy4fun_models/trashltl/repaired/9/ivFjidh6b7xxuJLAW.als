open main
pred idivFjidh6b7xxuJLAW_prop10 {
  always all f : File | f  in Protected implies f  in Protected
  
}
pred __repair { idivFjidh6b7xxuJLAW_prop10 }
check __repair { idivFjidh6b7xxuJLAW_prop10 <=> prop10o }