package main

import (
	"fmt"
	"math"
)

const (
	Big   = 1 << 100
	Small = Big >> 99
)

func needInt(x int) int {
	return x*10 + 1
}
func needFloat(x float64) float64 {
	return x * 0.1
}

func main() {
	fmt.Println(needInt(Small))
	fmt.Println(needFloat(Small))
	fmt.Println(needFloat(Big))

	var x, y int = 3, 4
	var f float64 = math.Sqrt(float64(x*x + y*y))
	var z uint = uint(f)
	fmt.Println(x, y, z)

	v := complex128(42.5)
	fmt.Printf("v is of type %T\n", v)

	const Truth = true
	const Pi = 3.14
	const World = "世界"
	fmt.Printf("Truth is of type %T\n", Truth)
	fmt.Printf("Pi is of type %T\n", Pi)
	fmt.Printf("World is of type %T\n", World)
}
