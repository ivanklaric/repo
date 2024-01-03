package main

import (
	"fmt"
	"math/rand"
	"time"
)

func main() {
	rand.Seed(time.Now().UnixNano())
	sum := 0
	for i := 0; i < 10; i++ {
		sum += i
	}
	fmt.Println(sum)

	for {
		sum -= rand.Intn(5)
		if sum <= 20 {
			break
		}
	}
	fmt.Println(sum)
}
