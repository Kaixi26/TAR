open main
pred idDAxygKKdopiNvoH6m_prop8 {
  always(all f: File | some f.link implies eventually f.link in Trash)
}
pred __repair { idDAxygKKdopiNvoH6m_prop8 }
check __repair { idDAxygKKdopiNvoH6m_prop8 <=> prop8o }