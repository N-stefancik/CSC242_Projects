import java.util.Scanner;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.ArrayList;

class GameState {
    public int[][] state;
    public int turn;
    public int player;
    
    public GameState(int[][] state, int turn, int player) {
        this.state = new int[state.length][state[0].length];
        for (int i = 0; i < state.length; i++) {
            this.state[i] = state[i].clone(); // Deep copy each row
        }
        this.turn = turn;
        this.player = player;
    }
}

public class Main {

    private static final int INFINITY = Integer.MAX_VALUE;
    private static final int NEG_INFINITY = Integer.MIN_VALUE;
    public static int stateArray[][];
    private static final long TIME_LIMIT = 900;
    private static final int MAX_DEPTH = 15; 
    public static long startTime;
    
    


    public static boolean isTimeUp() {
        return System.currentTimeMillis() - startTime >= TIME_LIMIT;
    }
    public static double miniMax(GameState gameState, int depth, double alpha, double beta, boolean maxPlayer) {
        // Base case: evaluate terminal states first
        if (isGameOver(gameState)) {
            // Use actual score difference for terminal states
            int finalScoreDiff = gameState.state[gameState.player][6] - gameState.state[1 - gameState.player][6];
            return finalScoreDiff > 0 ? 10000 + finalScoreDiff : -10000 + finalScoreDiff;
        }

        // Check other termination conditions
        if (isTimeUp() || depth == 0) {
            return heuristicFunction(gameState);
        }

        List<Integer> moves = possibleMoves(gameState);
        if (moves.isEmpty()) {
            return heuristicFunction(gameState);
        }

        if (maxPlayer) {
            double maxEval = NEG_INFINITY;
            for (int move : moves) {
                GameState newState = new GameState(gameState.state, gameState.turn, gameState.player);
                boolean extraTurn = simulateMove(newState, move);
                
                // If we get an extra turn, continue with maxPlayer true
                double val = miniMax(newState, depth - 1, alpha, beta, extraTurn);
                maxEval = Math.max(val, maxEval);
                alpha = Math.max(alpha, maxEval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            double minEval = INFINITY;
            for (int move : moves) {
                GameState newState = new GameState(gameState.state, gameState.turn, gameState.player);
                boolean extraTurn = simulateMove(newState, move);
                
                // If opponent gets an extra turn, continue with maxPlayer false
                double val = miniMax(newState, depth - 1, alpha, beta, !extraTurn);
                minEval = Math.min(val, minEval);
                beta = Math.min(beta, minEval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    // New method to simulate a move and return whether it results in an extra turn
    private static boolean simulateMove(GameState state, int move) {
        int player = state.player;
        Turn(player, move, state);
        return state.player == player; // If player hasn't changed, we got an extra turn
    }

    public static double heuristicFunction(GameState gameState) {
        int player = gameState.player;
        int opponent = 1 - player;
        
        // Base score from store difference
        double score = (gameState.state[player][6] - gameState.state[opponent][6]) * 2.0;
        
        // Count total seeds on each side
        int playerSeeds = 0;
        int opponentSeeds = 0;
        for (int i = 0; i < 6; i++) {
            playerSeeds += gameState.state[player][i];
            opponentSeeds += gameState.state[opponent][i];
        }
        
        // Add weight for seeds on our side
        score += playerSeeds * 0.5;
        score -= opponentSeeds * 0.5;
        
        // Check for capture opportunities
        for (int i = 0; i < 6; i++) {
            // Reward positions that can lead to captures
            if (gameState.state[player][i] == 0 && gameState.state[opponent][i] > 0) {
                // Look for seeds that could land in this empty pit
                for (int j = 0; j < 6; j++) {
                    if (gameState.state[player][j] == (i - j + 1)) {
                        score += gameState.state[opponent][i] * 1.5;
                    }
                }
            }
            
            // Penalize vulnerable positions
            if (gameState.state[player][i] > 0 && gameState.state[opponent][i] == 0) {
                for (int j = 0; j < 6; j++) {
                    if (gameState.state[opponent][j] == (i - j + 1)) {
                        score -= gameState.state[player][i] * 1.5;
                    }
                }
            }
        }
        
        // Bonus for moves that could lead to extra turns
        for (int i = 0; i < 6; i++) {
            if (gameState.state[player][i] == (6 - i)) {
                score += 3.0;
            }
        }
        
        return score;
    }

    public static int findBestMove(GameState gameState) {
        startTime = System.currentTimeMillis();
        int bestMove = -1;
        double bestValue = NEG_INFINITY;
        
        List<Integer> moves = possibleMoves(gameState);
        if (moves.isEmpty()) return -1;
        
        // Start with iterative deepening
        for (int depth = 1; depth <= MAX_DEPTH && !isTimeUp(); depth++) {
            for (int move : moves) {
                GameState newState = new GameState(gameState.state, gameState.turn, gameState.player);
                boolean extraTurn = simulateMove(newState, move);
                
                double value = miniMax(newState, depth - 1, NEG_INFINITY, INFINITY, extraTurn);
                
                if (value > bestValue || (value == bestValue && Math.random() < 0.1)) {
                    bestValue = value;
                    bestMove = move;
                }
            }
        }
        
        return bestMove;
    }

/* 
    public static int findBestMove(GameState gameState){
        startTime = System.currentTimeMillis();
        int bestMove = -INFINITY;  // Default to invalid move
        double bestValue = -INFINITY;
    
        List<Integer> moveList = possibleMoves(gameState);
        
        //System.out.println("Possible Moves: " + moveList);
        
        
    
        for (int i : moveList){
            GameState newState = new GameState(gameState.state, gameState.turn, gameState.player);
            
            // Apply the move before evaluation
            Turn(newState.player, i, newState);
    
            double value = miniMax(newState, MAX_DEPTH, NEG_INFINITY, INFINITY, false);
            //System.out.println("Value:" + value + " Move:" + i);
    
            // Pick the best move
            if(value > bestValue || bestValue == -INFINITY){
                bestValue = value;
                bestMove = i;
            }
            if(isTimeUp()){
                break;
            }
            
            
        }
    
        return bestMove;
    }

    
    public static double miniMax(GameState gameState, int depth, double alpha, double beta, boolean maxPlayer) {
        // Base case: Apply heuristic function at leaf nodes
        if (isTimeUp() || depth == MAX_DEPTH || isGameOver(gameState)) {
            if (isGameOver(gameState)) {
                return gameState.state[gameState.player][6] > gameState.state[1 - gameState.player][6] ? INFINITY : NEG_INFINITY;
            }
            return heuristicFunction(gameState);
        }
    
        List<Integer> moves = possibleMoves(gameState);
    
        // If no moves are possible, return heuristic (in case of terminal state)
        if (moves.isEmpty()) {
            return heuristicFunction(gameState);
        }
    
        if (maxPlayer) {
            double maxEval = NEG_INFINITY;
            for (int i : moves) {
                GameState newState = new GameState(gameState.state, gameState.turn, gameState.player);
                Turn(newState.player, i, newState);
                double val = miniMax(newState, depth + 1, alpha, beta, false);
                maxEval = Math.max(val, maxEval);
                alpha = Math.max(alpha, maxEval);
                if (beta <= alpha) {
                    break; // Alpha-beta pruning
                }
            }
            return maxEval;
        } else {
            double minEval = INFINITY;
            for (int i : moves) {
                GameState newState = new GameState(gameState.state, gameState.turn, gameState.player);
                Turn(newState.player, i, newState);
                double val = miniMax(newState, depth + 1, alpha, beta, true);
                minEval = Math.min(val, minEval);
                beta = Math.min(beta, minEval);
                if (beta <= alpha) {
                    break; // Alpha-beta pruning
                }
            }
            return minEval;
        }
    }
    */
    
    public static List<Integer> possibleMoves(GameState gameState){
        List<Integer> moves = new ArrayList<>();
        GameState newState = new GameState(gameState.state, gameState.turn, gameState.player);
        for (int i = 0; i < 6; i++) {
            if(newState.state[newState.player][i] != 0){
                moves.add(i+1);
            }
        }
        return moves;
    }
    public static GameState StateParser(String line) {
        String[] parts = line.split(" ");
         
        int N = Integer.parseInt(parts[1]);
            
        // Create the 2 x (N+1) array
        int[][] result = new int[2][N + 1];
     
        // Fill player 1's positions
        for (int i = 0; i < N; i++) {
            result[0][i] = Integer.parseInt(parts[i + 2]);
        }
     
        // Fill player 2's positions
        for (int i = 0; i < N; i++) {
            result[1][i] = Integer.parseInt(parts[i + N + 2]);
        }
     
        // Fill scores
        result[0][N] = Integer.parseInt(parts[2 * N + 2]); // p1S
        result[1][N] = Integer.parseInt(parts[2 * N + 3]); // p2S
     
        // Get turn and player from the last two elements
        int turn = Integer.parseInt(parts[2 * N + 4]);
        int player = Integer.parseInt(parts[2 * N + 5])-1;
     
        return new GameState(result, turn, player);
        
    }
    /* 
    public static GameState StateParser(String line) {
        // Split the input on <>
        String[] parts = line.split("[<>]");
        
        // Filter out empty strings
        parts = java.util.Arrays.stream(parts)
            .filter(s -> !s.trim().isEmpty())
            .toArray(String[]::new);
        
        try {
            // Get N from the second element (after "STATE") 
            int N = Integer.parseInt(parts[1]);
            
            // Create the 2 x (N+1) array
            int[][] result = new int[2][N + 1];
            
            // Fill player 1's positions
            for (int i = 0; i < N; i++) {
                result[0][i] = Integer.parseInt(parts[i + 2]);
            }
            
            // Fill player 2's positions
            for (int i = 0; i < N; i++) {
                result[1][i] = Integer.parseInt(parts[i + N + 2]);
            }
            
            // Fill scores
            result[0][N] = Integer.parseInt(parts[2 * N + 2]); // p1S
            result[1][N] = Integer.parseInt(parts[2 * N + 3]); // p2S
            
            // Get turn and player from the last two elements
            int turn = Integer.parseInt(parts[2 * N + 4]);
            int player = Integer.parseInt(parts[2 * N + 5]);
            
            return new GameState(result, turn, player);
            
        } catch (Exception e) {
            System.err.println("Error parsing input: " + e.getMessage());
            return null;
        }
    }
    */
    public static void Turn(int player, int pos, GameState gameState){
        int numSeeds = gameState.state[player][pos-1];
        int row = player;
        boolean droppedInStore = false; 
        
        gameState.state[player][pos-1] = 0;
        pos++;

        while(numSeeds > 0){
            // Different Conditions
                if (pos < 7){
                    if (numSeeds == 1 && gameState.state[row][pos-1] == 0 && row == player && gameState.state[1 - row][pos-1] != 0){
                        gameState.state[player][6] += gameState.state[1 - row][pos-1] + 1;
                        gameState.state[1 - row][pos-1] = 0;
                    }else{
                    gameState.state[row][pos-1] ++;
                    pos ++;
                    }
                }else if (pos == 7 && row == player){
                    gameState.state[row][6] ++;
                    if (row == 0){
                        row = 1;
                    }else{
                        row = 0;
                    }
                    if (numSeeds == 1){
                        droppedInStore = true;
                        
                    }
                    pos = 1;
                }else{
                    //didn't include capture here
                    if (row == 0){
                        row = 1;
                    }else{
                        row = 0;
                    }
                    gameState.state[row][0] ++;
                    pos = 2; 
                }
            numSeeds --;
            //System.out.println(pos);
        }

        boolean emptyRow = true;
        for (int i = 0; i < 6; i++) {
            if (gameState.state[player][i] != 0){
                emptyRow = false;
            }
        }
        if(emptyRow){
            for (int i = 0; i < 6; i++) {
                gameState.state[1-player][6] += gameState.state[1-player][i];
                gameState.state[1-player][i] = 0;
            } 
        }

        if( !droppedInStore ){
             
            if (player == 0){
                gameState.player = 1;
            }else{
                gameState.player = 0;
                gameState.turn++;
            } 
        }
        
        
        //System.out.println(gameState.player+ "player");
        //System.out.println();
        //System.out.println("New Turn");
    }
    
/* 
    public static double heuristicFunction(GameState gameState) {
        int scoreDiff = gameState.state[gameState.player][6] - gameState.state[1 - gameState.player][6];
        int extraTurnBonus = 0;
        int captureBonus = 0;
        int opponentCapturePenalty = 0;
    
        // Reward moves that lead to extra turns
        for (int i = 0; i < 6; i++) {
            if (gameState.state[gameState.player][i] == (6 - i)) { // Lands in store
                extraTurnBonus += 3; 
            }
        }
    
        // Reward captures
        for (int i = 0; i < 6; i++) {
            if (gameState.state[gameState.player][i] == 1 && gameState.state[1 - gameState.player][i] > 0) { // Capture condition
                captureBonus += gameState.state[1 - gameState.player][i] + 2; 
            }
        }
    
        // **NEW: Penalize board states where the opponent can easily capture**
        for (int i = 0; i < 6; i++) {
            if (gameState.state[1 - gameState.player][i] == 1 && gameState.state[gameState.player][i] > 0) { 
                // If opponent has a single stone and can capture from player
                opponentCapturePenalty += gameState.state[gameState.player][i] + 3;
            }
        }
    
        //return scoreDiff + 0.5 * extraTurnBonus + 0.7 * captureBonus - 0.7 * opponentCapturePenalty;
        return scoreDiff;
    }
    */

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        GameState state = StateParser(line);
    
        
            // Print current game state
            printGameState(state);
    
            // Check if the game is over
            
            int bestMove = findBestMove(state);
            //System.out.println("AI chooses move: " + bestMove);
            //Turn(state.player, bestMove, state);
            //System.out.println(state.player + " " + state.turn);
            if(state.player == 1 && state.turn == 2){
                System.out.println("PIE");
            }else{
            System.out.println(bestMove);
            }
            // Print updated game state
            //printGameState(state);
        
                // Check for game-ending conditions
            //if (isGameOver(state)) {
            //System.out.println("Game Over!");
            //break;
            //}

            
            /* 
            if(state.player == 0){
                int bestMove = findBestMove(state);
                //System.out.println("AI chooses move: " + bestMove);
                Turn(state.player, bestMove, state);
        
                // Print updated game state
                //printGameState(state);
        
                // Check for game-ending conditions
                if (isGameOver(state)) {
                //System.out.println("Game Over!");
                break;
                }

            }else{
            // Get user move
            //System.out.println("Player " + state.player + ", enter your move (1-6): ");
            int moveChoice;
            try {
                moveChoice = Integer.parseInt(scanner.nextLine());
                if (moveChoice < 1 || moveChoice > 6 || state.state[state.player][moveChoice - 1] == 0) {
                    //System.out.println("Invalid move. Try again.");
                    continue;
                }
            } catch (NumberFormatException e) {
                //System.out.println("Invalid input. Please enter a number between 1 and 6.");
                continue;
            }
    
            // Apply user's move
            Turn(state.player, moveChoice, state);
        }
            */
        scanner.close();
    }
    
    // Method to check if the game is over
    private static boolean isGameOver(GameState state) {
        boolean player1Empty = true, player2Empty = true;
        
        for (int i = 0; i < 6; i++) {
            if (state.state[0][i] != 0) player1Empty = false;
            if (state.state[1][i] != 0) player2Empty = false;
        }
        
        return player1Empty || player2Empty;
    }
    
    
    // Helper method to print the game state
    private static void printGameState(GameState gameState) {
        //System.out.println("State Array:");
        for (int i = 0; i < gameState.state.length; i++) {
            for (int j = 0; j < gameState.state[i].length; j++) {
                //System.out.print(gameState.state[i][j] + " ");
            }
            //System.out.println();
        }
        //System.out.println("Turn: " + gameState.turn);
        //System.out.println("Player: " + gameState.player);
    }
}