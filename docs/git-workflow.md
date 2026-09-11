# Git 워크플로우

`.claude/rules/git.md` 규칙을 실제로 따르는 순서 요약.

## 원칙

- `main` — 배포 가능 상태만 유지. **직접 커밋·푸시 금지**
- 기능 개발: `feature/<이슈번호>-<간단설명>`
- 버그 수정: `fix/<이슈번호>-<간단설명>`
- 이슈번호가 없다면 짧은 슬러그만 사용 (`feature/user-search`)

## 세팅 (최초 1회)

이 저장소를 처음 clone 한 뒤 한 번만 실행한다. `main` 직접 푸시를 막는 pre-push 훅을 활성화한다.

```bash
git config core.hooksPath .githooks
```

`core.hooksPath`는 `.git/config`(로컬)에 저장되므로 clone 마다 실행 필요.

## 기능 개발 사이클

```bash
# 1. main 최신화
git switch main
git pull

# 2. 브랜치 생성 및 전환
git switch -c feature/42-user-search

# 3. 작업 → 커밋 (원자 단위로 여러 개 가능)
git add <file>
git commit -m "feat: 사용자 검색 API 추가"

# 4. 원격에 푸시 (첫 푸시는 -u 로 upstream 설정)
git push -u origin feature/42-user-search

# 5. GitHub 에서 Pull Request 생성 → 리뷰 → 머지

# 6. 머지 완료 후 로컬 정리
git switch main
git pull
git branch -d feature/42-user-search        # 로컬 브랜치 삭제
git push origin --delete feature/42-user-search   # 원격도 필요하면
```

## PR 만들기 (gh CLI 사용 시)

```bash
gh pr create --title "feat: 사용자 검색 API 추가" \
             --body  "이슈 #42 관련. 검색 파라미터: q, page, size"
```

## 커밋 메시지

`.claude/rules/git.md` 참조. 요약:
- 형식: `<type>: <제목>` (50자 이내)
- type: `feat` `fix` `refactor` `docs` `test` `chore` `style` `perf`
- 제목은 명령형 어미 지양(한글: "추가", "수정" 같은 명사형)

## pre-push 훅 동작

`.githooks/pre-push` 가 다음 상황에서 푸시를 차단한다.
- 대상 브랜치가 `main` 또는 `master`

우회가 반드시 필요하면 (예: hotfix 한줄 픽스):
```bash
ALLOW_MAIN_PUSH=1 git push
```

## 자주 하는 실수

| 상황 | 대응 |
|---|---|
| `main` 에서 그대로 작업 시작해버림 | `git switch -c feature/xxx` — 커밋을 새 브랜치로 옮김 |
| `main` 에 실수로 커밋함 | `git switch -c feature/xxx` → `git switch main` → `git reset --hard origin/main` (⚠ 로컬 main 되돌림) |
| 훅이 안 걸림 | `git config --get core.hooksPath` 로 `.githooks` 반환되는지 확인 |
