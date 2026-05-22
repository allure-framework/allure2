export const MODAL_HEADER_ACTIONS_EVENT = "allure:modal-header-actions";

export type ModalHeaderActionsEventDetail = {
  actions: Element[];
};

export const createModalHeaderActionsEvent = (actions: Element[]) =>
  new CustomEvent<ModalHeaderActionsEventDetail>(MODAL_HEADER_ACTIONS_EVENT, {
    bubbles: true,
    cancelable: true,
    detail: {
      actions,
    },
  });
