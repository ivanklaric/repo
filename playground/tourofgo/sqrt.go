package main

import (
	"fmt"
	"math"
	"runtime"
)

func Sqrt(x float64) float64 {
	z := 1.0
	for diff := 10.0; math.Abs(diff) > 0.0001; {
		fmt.Println(z)
		diff = (z*z - x) / (2 * z)
		z -= diff
	}
	return z
}

func printos() {
	fmt.Print("Go runs on ")
	switch os := runtime.GOOS; os {
	case "darwin":
		fmt.Println("OS X")
	case "linux":
		fmt.Println("Linux")
	default:
		fmt.Printf("%s\n", os)
	}
}

func main() {
	defer printos()

	fmt.Println(Sqrt(100))
}
