package main

import (
	"fmt"
	"log"
	"net/http"
)

func main() {
	http.HandleFunc("/ping", func(w http.ResponseWriter, r *http.Request) {
		log.Println("Got request to /ping of /v2")
		fmt.Fprintf(w, "pong")
	})

	http.HandleFunc("/feature", func(w http.ResponseWriter, r *http.Request) {
		log.Println("Got request to /feature of /v2")
		if r.Header.Get("x-feature-enabled") == "true" {
			fmt.Fprintf(w, "Feature X is enabled!")
		} else {
			fmt.Fprintf(w, "Got your request, but feature X is disabled.")
		}
	})

	log.Println("Server runnning on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
