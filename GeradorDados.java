import java.io.*;
import java.util.Random;

public class GeradorDados {
    public static void main(String[] args) throws IOException {
        String arquivoSaida = "arquivoEntrada.txt";
        int totalLinhas = 5_000_000; // Ajuste conforme necessário

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida))) {
            Random rand = new Random();

            for (int i = 1; i <= totalLinhas; i++) {
                int id = rand.nextInt(5000000);
                String nome = "Pessoa";
                writer.write(id + ", " + nome);
                writer.newLine();

                // Mostrar progresso a cada 10.000 linhas
                if (i % 10_000 == 0) {
                    System.out.println("Geradas " + i + " linhas...");
                }
            }
        }

        System.out.println("Arquivo gerado com sucesso: " + arquivoSaida);
    }
}