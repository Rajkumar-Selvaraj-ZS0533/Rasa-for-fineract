#!/bin/bash
rasa run -m models --enable-api --cors * --debug &
rasa run actions
