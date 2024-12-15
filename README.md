# Wordle_solver overview
This project is a GUI-based Wordle game that allows users to play a self-solving version of the popular Wordle game. The application provides suggestions for possible words based on user feedback and includes interactive features for an engaging gameplay experience.

# Features
1.Self-Solver Mode:
> A 6x5 grid where users can make guesses.
> Colored feedback for each guess:
  >> Green: Correct letter in the correct position.

  >> Yellow: Correct letter in the wrong position.

  >> Grey: Incorrect letter.
> Suggestions for top 6 possible words after each guess.
> User-friendly error messages for invalid input.

2.Game Flow:
> Users have up to 6 attempts to guess the hidden word.
> Options to start a new game or quit after the game ends.
> Display of the hidden word if the user fails to guess correctly.

3.Trie-Based Word Management:
> Efficient storage and lookup of words.
> Filtering of possible words based on feedback.

4.Interactive GUI:
> On-screen keyboard and text input.
> Buttons for submitting guesses, starting a new game, or quitting.
> Dynamic grid and color changes for an engaging user experience.

5.File-Based Word List:
> Reads the word list dynamically from wordlist.txt.
> Ensures flexibility and extensibility for new word additions.
