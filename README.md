# External Merge Sort com Merge Sort e Quick Sort Three-way
Esse repositório apresenta a aplicação dos algorítmos de ordenação "External Sort com Merge Sort" e "Quick Sort Three-way" em uma base aleatória de dados não-ordenada. Como requisito para um trabalho matéria de Estruturas de Dados I, busca-se também analisar o consumo de CPU dos algorítmos, verificando como ambos se saem em diferentes situações
## Passo-a-passo para a execução
1. Gerar dados
2. Compilar o algorítmo de ordenação
    1. External Merge Sort com Merge Sort
    2. Quick Sort Three-way
3. Executar o algorítmo de ordenação
    1. External Merge Sort com Merge Sort
    2. Quick Sort Three-way

## Gerar dados
execute o comando
```
java GerardadosRefac
```
isso irá gerar o  `arquivoEntrada.txt`
## Compilação
#### External Merge Sort com Merge Sort

```
javac ExternalSort
```

#### Quick Sort Three-way
```
javac QuickSortThreeWay
```

## Execução
#### External Merge Sort com Merge Sort

```
java ExternalSort arquivoEntrada.txt arquivoSaida.txt
```

#### Quick Sort Three-way
```
java QuickSortThreeWay arquivoEntrada.txt arquivoSaida.txt
```


## Complexidade - External Merge Sort com Merge Sort
### Tempo
| caso  | complexidade |
| ------------- |:-------------:|
|melhor caso| O(n log​ (n/m​))|
|caso médio|O(n log​ (n/m​))|
|pior caso| O(n log n)|

### Memória
| caso  | complexidade |
| ------------- |:-------------:|
|caso médio|O(M+k⋅B)|
|pior caso|O(M+k⋅B)|


## Complexidade - Quick Sort Three-way

### Tempo
| caso  | complexidade |
| ------------- |:-------------:|
|melhor caso|O(n log n)|
|caso médio|O(n log n)|
|pior caso|O(n²)|


### Memória
| caso  | complexidade |
| ------------- |:-------------:|
|caso médio|O(n log n)|
|pior caso|O(n)|
