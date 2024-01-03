package main

import "fmt"

var packagevar string = "foo"

func add(x, y int) int {
	return x + y
}

func swap(s1, s2 string) (r1, r2 string) {
	r1 = s2
	r2 = s1
	return
}

func main() {
	fmt.Println(add(42, 13))

	packagevar = "package-level variable"
	var hello string
	hello = "hello"
	world := "world"
	fmt.Println(swap(hello, world))
	fmt.Println(packagevar)
}
