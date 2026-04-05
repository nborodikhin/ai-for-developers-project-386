import { Routes, Route } from 'react-router-dom';
import { AppShell } from '@mantine/core';
import { Navbar } from './components/Navbar';
import { HomePage } from './pages/HomePage';
import { BookCatalogPage } from './pages/BookCatalogPage';
import { BookEventPage } from './pages/BookEventPage';
import { AdminPage } from './pages/AdminPage';

export default function App() {
  return (
    <AppShell header={{ height: 60 }}>
      <AppShell.Header>
        <Navbar />
      </AppShell.Header>
      <AppShell.Main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/book" element={<BookCatalogPage />} />
          <Route path="/book/:id" element={<BookEventPage />} />
          <Route path="/admin" element={<AdminPage />} />
        </Routes>
      </AppShell.Main>
    </AppShell>
  );
}
