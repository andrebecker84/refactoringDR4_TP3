#!/usr/bin/env bash
set -euo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; CYAN='\033[0;36m'; RESET='\033[0m'

check_prerequisites() {
    command -v java >/dev/null 2>&1 || { echo -e "${RED}[ERRO]${RESET} Java não encontrado. Instale o JDK 21."; exit 1; }
    command -v mvn  >/dev/null 2>&1 || { echo -e "${RED}[ERRO]${RESET} Maven não encontrado. Instale o Maven 3.9+."; exit 1; }
}

open_report() {
    local report="target/site/jacoco/index.html"
    if [ ! -f "$report" ]; then
        echo -e "${RED}Relatório não encontrado.${RESET} Execute a opção [3] primeiro."
        return
    fi
    if command -v xdg-open >/dev/null 2>&1; then
        xdg-open "$report"
    elif command -v open >/dev/null 2>&1; then
        open "$report"
    else
        echo "Abra manualmente: $report"
    fi
}

menu() {
    while true; do
        clear
        echo -e "${CYAN}============================================================${RESET}"
        echo -e "${CYAN}  refactoringDR4_TP3 -- E-Commerce Order Refactoring${RESET}"
        echo -e "${CYAN}============================================================${RESET}"
        echo
        echo "  [1] Compilar"
        echo "  [2] Executar testes"
        echo "  [3] Build completo + cobertura JaCoCo"
        echo "  [4] Abrir relatório JaCoCo"
        echo "  [5] Limpar build"
        echo "  [0] Sair"
        echo
        read -rp "Escolha: " opcao
        case "$opcao" in
            1) mvn clean compile; read -rp "Pressione Enter..." ;;
            2) mvn test; read -rp "Pressione Enter..." ;;
            3) mvn clean verify; read -rp "Pressione Enter..." ;;
            4) open_report; read -rp "Pressione Enter..." ;;
            5) mvn clean; echo -e "${GREEN}Build removido.${RESET}"; read -rp "Pressione Enter..." ;;
            0) exit 0 ;;
            *) echo "Opção inválida." ;;
        esac
    done
}

check_prerequisites
menu