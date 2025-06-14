#!/bin/bash

# Определяем директорию, где находится скрипт
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Запускаем приложение
"$SCRIPT_DIR/target/streamapp-runtime/bin/streamapp" 