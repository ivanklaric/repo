package main

import (
	"io"
	"os"
	"strings"
)

type InfiniteAs struct{}

func (i InfiniteAs) Read(b []byte) (int, error) {
	bytesGenerated := 0
	for ; bytesGenerated < len(b); bytesGenerated++ {
		b[bytesGenerated] = 'A'
	}
	return bytesGenerated, nil
}

type rot13Reader struct {
	r io.Reader
}

func (r13r rot13Reader) Read(b []byte) (int, error) {
	inputLookup := "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
	outputLookup := "NOPQRSTUVWXYZABCDEFGHIJKLMnopqrstuvwxyzabcdefghijklm"
	n, e := r13r.r.Read(b)
	for i, _ := range b {
		index := strings.IndexByte(inputLookup, b[i])
		if index >= 0 {
			b[i] = outputLookup[strings.IndexByte(inputLookup, b[i])]
		}
	}
	return n, e
}

func main() {
	s := strings.NewReader("Lbh penpxrq gur pbqr!")
	r := rot13Reader{s}
	io.Copy(os.Stdout, &r)
}
