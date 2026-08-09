import { Modal as HeroModal, useOverlayState } from '@heroui/react'
import { ReactNode, createContext, useContext } from 'react'
import clsx from 'clsx'

type CloseFn = () => void

const ModalCloseContext = createContext<CloseFn>(() => undefined)

const MODAL_SIZE_CLASSES = {
  xs: 'max-w-xs',
  sm: 'max-w-sm',
  md: 'max-w-md',
  lg: 'max-w-lg',
  xl: 'max-w-xl',
  '2xl': 'max-w-2xl',
  '3xl': 'max-w-3xl',
  '4xl': 'max-w-4xl',
  '5xl': 'max-w-5xl',
  cover: 'max-h-[calc(100dvh-2rem)] max-w-[calc(100dvw-2rem)]',
  full: 'h-[calc(100dvh-2rem)] max-w-[calc(100dvw-2rem)]'
} as const

type ModalSize = keyof typeof MODAL_SIZE_CLASSES
type ModalBackdrop = 'blur' | 'opaque' | 'transparent'

export type ModalProps = Readonly<{
  backdrop?: ModalBackdrop
  children?: ReactNode
  className?: string
  defaultOpen?: boolean
  isOpen?: boolean
  onClose?: CloseFn
  onOpenChange?: (isOpen: boolean) => void
  size?: ModalSize
  state?: ReturnType<typeof useOverlayState>
}>

export function useDisclosure(defaultOpen = false) {
  const state = useOverlayState({ defaultOpen })
  return {
    isOpen: state.isOpen,
    onOpen: state.open,
    onClose: state.close,
    onOpenChange: state.setOpen
  }
}

export function Modal(props: ModalProps) {
  const { backdrop, children, className, defaultOpen, isOpen, onClose, onOpenChange, size = 'md', state } = props
  const internalState = useOverlayState({
    defaultOpen,
    isOpen,
    onOpenChange: (open) => {
      onOpenChange?.(open)
      if (!open) onClose?.()
    }
  })
  const modalState = state ?? internalState

  return (
    <HeroModal state={modalState}>
      <HeroModal.Backdrop variant={backdrop}>
        <HeroModal.Container>
          <HeroModal.Dialog
            className={clsx(
              'mx-4 w-full max-h-[90dvh] overflow-auto',
              MODAL_SIZE_CLASSES[size],
              className
            )}
          >
            <ModalCloseContext.Provider value={modalState.close}>
              {typeof children === 'function'
                ? (children as (onClose: CloseFn) => ReactNode)(modalState.close)
                : children}
            </ModalCloseContext.Provider>
          </HeroModal.Dialog>
        </HeroModal.Container>
      </HeroModal.Backdrop>
    </HeroModal>
  )
}

export type ModalContentProps = Readonly<{
  children?: ReactNode | ((onClose: CloseFn) => ReactNode)
  className?: string
}>

export function ModalContent(props: ModalContentProps) {
  const close = useContext(ModalCloseContext)
  return (
    <div className={props.className}>
      {typeof props.children === 'function' ? props.children(close) : props.children}
    </div>
  )
}

export type ModalSlotProps = Readonly<{ children?: ReactNode; className?: string }>

export const ModalHeader = (props: ModalSlotProps) => (
  <HeroModal.Header className={props.className}>
    <HeroModal.Heading>{props.children}</HeroModal.Heading>
  </HeroModal.Header>
)

export const ModalBody = (props: ModalSlotProps) => (
  <HeroModal.Body className={clsx('overflow-visible px-1 pb-1', props.className)}>
    {props.children}
  </HeroModal.Body>
)

export const ModalFooter = (props: ModalSlotProps) => (
  <HeroModal.Footer className={props.className}>{props.children}</HeroModal.Footer>
)
