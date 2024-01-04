package main

import (
	"strings"

	"golang.org/x/tour/wc"
)

func WordCount(s string) map[string]int {
	ret := make(map[string]int)
	for _, str := range strings.Fields(s) {
		cnt := ret[str]
		ret[str] = cnt + 1
	}
	return ret
}

func main() {
	wc.Test(WordCount)
}
