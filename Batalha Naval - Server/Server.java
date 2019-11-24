/**
 * Trabalho Pratico - Redes I Frederico Oliveira e João Victor BATALHA NAVAL
 */
// Classe Server

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;

enum TipoNavio {
    PORTA_AVIAO, CONTRATORPEDEIRO, NAVIO_TANQUE, SUBMARINO,
}

enum OrientacaoNavio {
    HORIZONTAL, VERTICAL,
}

class Posicao {
    int linha;
    int coluna;

    Posicao(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
    }
}

class Navio {
    TipoNavio tipo;
    String orientacao;
    Posicao posicaoInicial;
    int tamanho;

    Navio(TipoNavio tipo, String orientacao, Posicao posicaoInicial) {
        this.tipo = tipo;
        this.orientacao = orientacao;
        this.posicaoInicial = posicaoInicial;
        switch (this.tipo) {
        case PORTA_AVIAO:
            this.tamanho = 5;
            break;
        case CONTRATORPEDEIRO:
            this.tamanho = 4;
            break;
        case NAVIO_TANQUE:
            this.tamanho = 3;
            break;
        case SUBMARINO:
            this.tamanho = 2;
            break;
        default:
            this.tamanho = -1;
            break;
        }
    }
}

class Tabuleiro {
    public int[][] tabuleiroAtual;

    // Codigo de posicao:
    // 0: Agua
    // 1: Barco
    // -1: Barco Danificado
    public Tabuleiro() {
        tabuleiroAtual = new int[10][10];
        for (int i = 0; i < tabuleiroAtual.length; i++) {
            for (int j = 0; j < tabuleiroAtual.length; j++) {
                tabuleiroAtual[i][j] = 0;
            }
        }
    }

    // Coloca os navios de entrada no tabuleiro
    public void colocarNavios(Navio[] navios) {
        for (int i = 0; i < navios.length; i++) {
            for (int j = 0; j < navios[i].tamanho; j++) {
                if (navios[i].orientacao.equals("HORIZONTAL")) {
                    tabuleiroAtual[navios[i].posicaoInicial.linha][navios[i].posicaoInicial.coluna + j] = 1;
                } else {
                    tabuleiroAtual[navios[i].posicaoInicial.linha + j][navios[i].posicaoInicial.coluna] = 1;
                }
            }
        }
    }

    // Metodo que imprime o tabuleiro
    public void printTabuleiro() {
        for (int i = 0; i < tabuleiroAtual.length; i++) {
            for (int j = 0; j < tabuleiroAtual[i].length; j++) {
                System.out.print(tabuleiroAtual[i][j]);
                System.out.print(' ');
            }
            System.out.println();
        }
    }

    // Retorna o objeto Tabuleiro:
    public int[][] getTabuleiro() {
        return tabuleiroAtual;
    }

    public void computarAcerto(int linha, int coluna) {
        tabuleiroAtual[linha][coluna] = -1;
    }

    public boolean checarVitoria() {
        boolean perdeu = true;
        for (int i = 0; i < tabuleiroAtual.length; i++) {
            for (int j = 0; j < tabuleiroAtual[i].length; j++) {
                if (tabuleiroAtual[i][j] == 1) {
                    perdeu = false;
                    return perdeu;
                }
            }
        }
        return perdeu;
    }
}

public class Server {
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(args[0]));
            String linhaAtual;
            ArrayList<Navio> navios = new ArrayList<Navio>();
            while ((linhaAtual = fileReader.readLine()) != null) {
                String[] parametros = linhaAtual.split(" ");
                TipoNavio tipo = parametros[0].equals("1") ? TipoNavio.PORTA_AVIAO
                        : parametros[0].equals("2") ? TipoNavio.CONTRATORPEDEIRO
                                : parametros[0].equals("3") ? TipoNavio.NAVIO_TANQUE : TipoNavio.SUBMARINO;
                String orientacao = parametros[1];
                Posicao posicaoInicial = new Posicao(Integer.parseInt(parametros[2]), Integer.parseInt(parametros[3]));
                navios.add(new Navio(tipo, orientacao, posicaoInicial));

            }

            // Cria o tabuleiro e posiciona os navios e o imprime na tela:
            Tabuleiro tabuleiro = new Tabuleiro();
            tabuleiro.colocarNavios(navios.toArray(new Navio[navios.size()]));
            tabuleiro.printTabuleiro();

            ServerSocket socketEndereco = new ServerSocket(6789);
            Socket serverSocket = socketEndereco.accept();

            // Leitor de dados do cliente:
            BufferedReader inputCliente = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            // Envia dados para o cliente:
            DataOutputStream outputCliente = new DataOutputStream(serverSocket.getOutputStream());

            boolean fimJogo = false;

            while (!fimJogo) {

                String jogadaCliente = inputCliente.readLine();
                System.out.println("Cliente tentou acertar: " + jogadaCliente);

                int linhaHitClient, colunaHitClient;
                linhaHitClient = Integer.parseInt(jogadaCliente.split(" ")[0]);
                colunaHitClient = Integer.parseInt(jogadaCliente.split(" ")[1]);

                if (tabuleiro.getTabuleiro()[linhaHitClient][colunaHitClient] == 1) {
                    tabuleiro.computarAcerto(linhaHitClient, colunaHitClient);
                    outputCliente.writeBytes("acertou" + '\n');
                    outputCliente.flush();
                } else {
                    outputCliente.writeBytes("errou" + '\n');
                    outputCliente.flush();
                }

                if (tabuleiro.checarVitoria()) {
                    fimJogo = true;
                    System.out.println("Voce perdeu.");
                    outputCliente.writeBytes("Voce venceu!" + '\n');
                    outputCliente.flush();
                }

                // Jogada do servidor:
                String jogadaServidor = scan.nextLine();
                outputCliente.writeBytes(jogadaServidor + "\n");
                outputCliente.flush();

                String resultadoCliente = inputCliente.readLine();
                System.out.println("Cliente disse: Voce " + resultadoCliente);

                tabuleiro.printTabuleiro();
            }

            // Fechar leitores de entrada
            try {
                serverSocket.close();
                socketEndereco.close();
                scan.close();
                fileReader.close();
            } catch (Exception e) {
                System.out.println(e);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
