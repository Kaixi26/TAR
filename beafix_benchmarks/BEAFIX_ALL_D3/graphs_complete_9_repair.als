//Line and OP: Line 123 <BES> 
//ORIGINAL: ((a in (a . ^ adj)) => (a in (a . ^ adj)))
//MUTATION: (a in (a . ^ adj))
//Line and OP: Line 123 <RUOD> 
//ORIGINAL: ^ adj
//MUTATION: adj
//Line and OP: Line 123 <VCR> 
//ORIGINAL: a
//MUTATION: Node
open util/integer as integer


sig Node {
	adj : set Node
}

pred undirected []{
	adj = (~ adj)
}

pred oriented []{
	no (adj & (~ adj))
}

pred acyclic []{
	all a : Node | a !in (a . (^ adj))
}

pred complete []{
	all a : Node | Node in (a . adj)
}

pred noLoops []{
	no (iden & adj)
}

pred weaklyConnected []{
	all n : Node | Node in (n . (* (adj + (~ adj))))
}

pred stonglyConnected []{
	all n : Node | Node in (n . (* adj))
}

pred transitive []{
	(adj . adj) in adj
}

pred undirectedOK []{
	adj = (~ adj)
}

pred orientedOK []{
	no (adj & (~ adj))
}

pred acyclicOK []{
	all a : Node | a !in (a . (^ adj))
}

pred completeOK []{
	all n : Node | Node in (n . adj)
}

pred noLoopsOK []{
	no (iden & adj)
}

pred weaklyConnectedOK []{
	all n : Node | Node in (n . (* (adj + (~ adj))))
}

pred stonglyConnectedOK []{
	all n : Node | Node in (n . (* adj))
}

pred transitiveOK []{
	(adj . adj) in adj
}

assert undirectedRepaired {
	undirected[] <=> undirectedOK[]
}

assert orientedRepaired {
	oriented[] <=> orientedOK[]
}

assert acyclicRepaired {
	acyclic[] <=> acyclicOK[]
}

assert completeRepaired {
	complete[] <=> completeOK[]
}

assert noLoopsRepaired {
	noLoops[] <=> noLoopsOK[]
}

assert weaklyConnectedRepaired {
	weaklyConnected[] <=> weaklyConnectedOK[]
}

assert stonglyConnectedRepaired {
	stonglyConnected[] <=> stonglyConnectedOK[]
}

assert transitiveRepaired {
	transitive[] <=> transitiveOK[]
}

check undirectedRepaired expect 0
check orientedRepaired expect 0
check acyclicRepaired expect 0
check completeRepaired expect 0
check noLoopsRepaired expect 0
check weaklyConnectedRepaired expect 0
check stonglyConnectedRepaired expect 0
check transitiveRepaired expect 0

