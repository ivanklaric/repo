package main

import (
	"fmt"
	"io"
)

type InfiniteAs struct{}

func (i InfiniteAs) Read(b []byte) (int, error) {
	bytesGenerated := 0
	for ; bytesGenerated < len(b); bytesGenerated++ {
		b[bytesGenerated] = 'A'
	}
	return bytesGenerated, nil
}

func main() {
	r := InfiniteAs{}

	b := make([]byte, 8)
	for {
		n, err := r.Read(b)
		fmt.Printf("n = %v err = %v b = %v\n", n, err, b)
		fmt.Printf("b[:n] = %q\n", b[:n])
		if err == io.EOF {
			break
		}
	}
}
