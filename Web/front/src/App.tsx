import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline, GlobalStyles } from '@mui/material';
import CreatePage from './pages/CreatePage';
import ShopPage from './pages/ShopPage';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#60a5fa', // Light blue
    },
    secondary: {
      main: '#c084fc', // Light purple
    },
    background: {
      default: '#0f172a', // Slate 900
      paper: '#1e293b',   // Slate 800
    },
    text: {
      primary: '#f8fafc',
      secondary: '#94a3b8',
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontWeight: 700,
    },
    h2: {
      fontWeight: 600,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: '0.5rem',
          fontWeight: 600,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <GlobalStyles
        styles={{
          body: {
            background: 'linear-gradient(to bottom right, #0f172a, #1e1b4b)',
            minHeight: '100vh',
          },
        }}
      />
      <BrowserRouter>
        <Routes>
          <Route path="/create" element={<CreatePage />} />
          <Route path="/shop/:userId" element={<ShopPage />} />
          <Route path="/" element={<></>} />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
