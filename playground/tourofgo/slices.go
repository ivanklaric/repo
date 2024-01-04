package main

import "fmt"

func printSlice(slice []int) {
	fmt.Printf("len=%d cap=%d %v\n", len(slice), cap(slice), slice)
}

func main() {
	a := make([]int, 5)
	printSlice(a)

	a = append(a, 0)
	printSlice(a)
	a = append(a, 1)
	printSlice(a)

	b := make([]int, 0, 5)
	printSlice(b)

	c := b[:2]
	printSlice(c)

	d := c[2:5]
	printSlice(d)

	var pow = []int{1, 2, 4, 8, 16, 32, 64, 128}
	for i, v := range pow {
		fmt.Printf("2^%d = %d\n", i, v)
		v++
	}
	printSlice(pow)

}
