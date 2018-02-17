import * as React from "react";
import * as bem from "b_";
import Sorter from "../Sorter";

const b = bem.with("SorterGroup");

interface SorterGroupProps {
  sorters: Array<{ id: string; name: string }>;
  onSorterChange: (id: string, asc: boolean) => void;
}

interface SorterGroupState {
  enabledSorterId?: string;
  enabledSorterAsc: boolean;
}

export default class SorterGroup extends React.Component<SorterGroupProps, SorterGroupState> {
  state = {
    enabledSorterId: undefined,
    enabledSorterAsc: true,
  };

  handleSorterChange = (id: string) => {
    this.setState(prevState => ({
      enabledSorterId: id,
      enabledSorterAsc: !prevState.enabledSorterAsc,
    }));

    const { onSorterChange } = this.props;
    const { enabledSorterAsc } = this.state;
    onSorterChange(id, enabledSorterAsc);
  };

  render() {
    const { sorters } = this.props;
    const { enabledSorterId, enabledSorterAsc } = this.state;
    return (
      <div className={b()}>
        {sorters.map(({ id, name }) => (
          <Sorter
            key={id}
            id={id}
            name={name}
            asc={enabledSorterAsc}
            enabled={id === enabledSorterId}
            onSorterClick={this.handleSorterChange}
          />
        ))}
      </div>
    );
  }
}
