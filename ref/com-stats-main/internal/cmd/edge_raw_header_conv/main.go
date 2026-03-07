package main

import (
	"bufio"
	"fmt"
	"os"

	"gopkg.in/yaml.v3"
)

func main() {
	name := "2026-01-24.headers"
	file, err := os.Open(name)
	if err != nil {
		panic(err)
	}
	defer func() {
		_ = file.Close()
	}()

	nameNoExt := name[:len(name)-len(".headers")]

	var key *string
	headers := make(map[string]string)

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		if len(line) == 0 {
			continue
		}

		if key == nil {
			key = &line
		} else {
			headers[*key] = line
			key = nil
		}
	}

	err = scanner.Err()
	if err != nil {
		panic(err)
	}

	bytes, err := yaml.Marshal(headers)
	if err != nil {
		return
	}

	err = os.WriteFile(fmt.Sprintf("%s.yml", nameNoExt), bytes, 0644)
	if err != nil {
		panic(err)
	}
}
