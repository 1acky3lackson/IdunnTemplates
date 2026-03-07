package service

import "net/http"

var (
	client *http.Client
)

func Init() {
	client = &http.Client{}
}
