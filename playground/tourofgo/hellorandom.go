package main

import (
	"fmt"
	"math"
	"math/rand"
	"time"
)

func main() {
	rand.Seed(time.Now().UnixNano())
	fmt.Println("Random numbers:", rand.Intn(10), rand.Intn(10), rand.Intn(10), rand.Intn(10))
	fmt.Println("Pi:", math.Pi)
}
