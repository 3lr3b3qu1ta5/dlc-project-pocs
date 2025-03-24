// Easy fibonacci n-term calculation

function fibonacci(n) {
    if (n <= 1) {
      return 1;
    } else {
      return n + fibonacci(n - 1);
    }
  }

fibonacci(10);
