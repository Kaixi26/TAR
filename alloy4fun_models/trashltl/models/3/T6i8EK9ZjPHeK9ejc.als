open main
pred idT6i8EK9ZjPHeK9ejc_prop4 {
  	some f: File | (always f not in Protected) implies eventually (always f in Trash) and eventually (always f.link in Trash)

}
pred __repair { idT6i8EK9ZjPHeK9ejc_prop4 }
check __repair { idT6i8EK9ZjPHeK9ejc_prop4 <=> prop4o }