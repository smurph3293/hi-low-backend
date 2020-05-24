#!/bin/bash

#======================================================================
# Merchant Unit Tests
#======================================================================

set -e
base_dir=$(pwd)
KCYN='\e[1;36m%-6s\e[m'
RED='\e[1;31m%-6s\e[m'

envs=(
    "team"
)

function print_color_message {
    printf $1 "$2"$'\n'
}

function error {
    message=$'\n'"    Error: $1"$'\n'
    print_color_message $RED "$message"
    display_usage
    exit -1
}

function display_usage {
	  print_color_message $KCYN $'\nUSAGE: \n\n    ./deploy.sh -env [] [OPTIONS]\n'
    printf "OPTIONS: \n\n    -tests        :will only run mvn package and not deploy\n\n"
    printf "EXAMPLE: \n\n    ./deploy.sh -env sean\n\n"
    echo_valid_envs
    printf "\n\n"
}

function echo_valid_envs {
    printf "\nvalid envs: \n\n"
    for val in ${envs[@]}; do
        printf "    $val\n"
    done
}

while test $# -gt 0; do
    case "$1" in
        -tests)
            shift
            tests='true'
            ;;
        -env)
            shift
            env=$1
            shift
            ;;
        *)
            error "Invalid option: $1"
            exit -1;;
    esac
done

if [[ $tests == 'true' ]]; then
    cd $base_dir/local
    print_color_message $KCYN $'\nSpinning up postgres container'
    docker-compose up -d -V hi_low_db
    print_color_message $KCYN $'\nRunning mvn clean package\n'
    cd $base_dir
    mvn clean package
    exit
fi

[[ -n "$env" ]] || error "Missing environment name"

# run deploy

