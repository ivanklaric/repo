package main

import "fmt"

func fibonacci() func() int {
	one_before, last := -1, -1
	return func() int {
		ret := 0

		if last == -1 {
			last = 0
			return 0
		}
		if last == 0 {
			ret = 1
		} else {
			ret = last + one_before
		}
		one_before = last
		last = ret
		return ret
	}
}

func main() {
	f := fibonacci()
	for i := 0; i < 10; i++ {
		fmt.Println(f())
	}
}
