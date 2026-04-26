import { render, type RenderOptions } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { MemoryRouter, type MemoryRouterProps } from 'react-router-dom';
import type { ReactElement } from 'react';

interface Options extends RenderOptions {
  routerProps?: MemoryRouterProps;
}

export function renderWithProviders(ui: ReactElement, options: Options = {}) {
  const { routerProps, ...renderOptions } = options;
  return render(
    <MemoryRouter {...routerProps}>
      <MantineProvider>
        {ui}
      </MantineProvider>
    </MemoryRouter>,
    renderOptions,
  );
}
