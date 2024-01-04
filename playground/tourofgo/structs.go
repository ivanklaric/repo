package main

import "fmt"

type Vertex struct {
	X int
	Y int
}

var (
	globalVertex = Vertex{X: 3}
)

func main() {
	fmt.Println(globalVertex)
	v := Vertex{1, 2}
	v.X = 4

	p := &v
	p.X = 1e9
	fmt.Println(v)

	var a [2]string
	a[0] = "Hello"
	a[1] = "World"
	fmt.Println(a)

	primes := [6]int{2, 3, 5, 7, 11}
	fmt.Println(primes)

	fmt.Printf("Type of primes is %T\n", primes)

	for i := 0; i < len(primes); i++ {
		fmt.Printf("prime number %d is %d\n", i+1, primes[i])
	}

	var slice []int = primes[1:4]
	fmt.Println(slice)
}
