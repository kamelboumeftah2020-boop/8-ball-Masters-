import { Component, type ReactNode } from "react";

interface State {
  error: Error | null;
}

/** Keeps a crashing page from blanking the whole app; shows the error and a way back. */
export class ErrorBoundary extends Component<{ children: ReactNode; resetKey?: string }, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidUpdate(prev: { resetKey?: string }) {
    if (this.state.error && prev.resetKey !== this.props.resetKey) this.setState({ error: null });
  }

  componentDidCatch(error: Error) {
    console.error("Page crashed:", error);
  }

  render() {
    if (!this.state.error) return this.props.children;
    return (
      <div className="state empty">
        <h3>حدث خطأ في هذه الصفحة</h3>
        <p>
          <small className="error-detail" dir="ltr">
            {this.state.error.message}
            <br />
            {(this.state.error.stack || "").split("\n").slice(0, 4).join("\n")}
          </small>
        </p>
        <button className="btn ghost" onClick={() => this.setState({ error: null })}>إعادة المحاولة</button>
      </div>
    );
  }
}
